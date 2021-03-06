/**
 * Hidden Switch Spellsource API
 * The Spellsource API for matchmaking, user accounts, collections management and more
 *
 * OpenAPI spec version: 1.0.1
 * Contact: benjamin.s.berman@gmail.com
 *
 * NOTE: This class is auto generated by the swagger code generator program.
 * https://github.com/swagger-api/swagger-codegen.git
 * Do not edit the class manually.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package com.hiddenswitch.spellsource.client.models;

import java.util.Objects;
import com.google.gson.annotations.SerializedName;
import com.hiddenswitch.spellsource.client.models.TargetActionPair;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;

import java.io.Serializable;
/**
 * A spell action or battlecry. 
 */
@ApiModel(description = "A spell action or battlecry. ")

public class SpellAction  implements Serializable {
  @SerializedName("sourceId")
  private Integer sourceId = null;

  @SerializedName("action")
  private Integer action = null;

  @SerializedName("targetKeyToActions")
  private List<TargetActionPair> targetKeyToActions = new ArrayList<TargetActionPair>();

  public SpellAction sourceId(Integer sourceId) {
    this.sourceId = sourceId;
    return this;
  }

   /**
   * The ID of the source card in your hand or the minion that originates this spell or battlecry. 
   * @return sourceId
  **/
  @ApiModelProperty(example = "null", value = "The ID of the source card in your hand or the minion that originates this spell or battlecry. ")
  public Integer getSourceId() {
    return sourceId;
  }

  public void setSourceId(Integer sourceId) {
    this.sourceId = sourceId;
  }

  public SpellAction action(Integer action) {
    this.action = action;
    return this;
  }

   /**
   * The action for this spell. Defined when this spell is not targetable. 
   * @return action
  **/
  @ApiModelProperty(example = "null", value = "The action for this spell. Defined when this spell is not targetable. ")
  public Integer getAction() {
    return action;
  }

  public void setAction(Integer action) {
    this.action = action;
  }

  public SpellAction targetKeyToActions(List<TargetActionPair> targetKeyToActions) {
    this.targetKeyToActions = targetKeyToActions;
    return this;
  }

  public SpellAction addTargetKeyToActionsItem(TargetActionPair targetKeyToActionsItem) {
    this.targetKeyToActions.add(targetKeyToActionsItem);
    return this;
  }

   /**
   * An array of entity ID-action pairs that let you convert a valid target to an action index to respond with. Defined if this spell is targetable. 
   * @return targetKeyToActions
  **/
  @ApiModelProperty(example = "null", value = "An array of entity ID-action pairs that let you convert a valid target to an action index to respond with. Defined if this spell is targetable. ")
  public List<TargetActionPair> getTargetKeyToActions() {
    return targetKeyToActions;
  }

  public void setTargetKeyToActions(List<TargetActionPair> targetKeyToActions) {
    this.targetKeyToActions = targetKeyToActions;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SpellAction spellAction = (SpellAction) o;
    return Objects.equals(this.sourceId, spellAction.sourceId) &&
        Objects.equals(this.action, spellAction.action) &&
        Objects.equals(this.targetKeyToActions, spellAction.targetKeyToActions);
  }

  @Override
  public int hashCode() {
    return Objects.hash(sourceId, action, targetKeyToActions);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SpellAction {\n");
    
    sb.append("    sourceId: ").append(toIndentedString(sourceId)).append("\n");
    sb.append("    action: ").append(toIndentedString(action)).append("\n");
    sb.append("    targetKeyToActions: ").append(toIndentedString(targetKeyToActions)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(java.lang.Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}

